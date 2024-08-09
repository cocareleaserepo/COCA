import re
import os
import sys
import argparse
sys.setrecursionlimit(1000000)


class template_tree(object):
    def __init__(self):
        self.prefix_tree = None
        self.template_list = []


    def match_event(self, log_list):
        return tree_match(self.prefix_tree, log_list)

    def add_templates(self, templates):
        self.template_list = templates
        if self.prefix_tree is None:
            self.prefix_tree = {}
            self.prefix_tree["$NO_STAR$"] = {}
        
        for event_id, event_template in enumerate(templates):
            # Full match
            if "<*>" not in event_template:
                self.prefix_tree["$NO_STAR$"][event_template] = event_template
                continue
            template_tokens = message_split(event_template)
            if not template_tokens or event_template=="<*>": continue

            start_token = template_tokens[0]
            if start_token not in self.prefix_tree:
                self.prefix_tree[start_token] = {}
            move_tree = self.prefix_tree[start_token]

            tidx = 1
            while tidx < len(template_tokens):
                token = template_tokens[tidx]
                if token not in move_tree:
                    move_tree[token] = {}
                move_tree = move_tree[token]
                tidx += 1
            # move_tree["".join(template_tokens)] = (len(template_tokens), template_tokens.count("<*>")) # length, count of <*>
            move_tree[event_template] = (len(template_tokens), template_tokens.count("<*>")) # length, count of <*>
        return

def post_process_tokens(tokens, punc):
    excluded_str = ['=', '|', '(', ')']
    for i in range(len(tokens)):
        if tokens[i].find("<*>") != -1:
            tokens[i] = "<*>"
        else:
            new_str = ""
            for s in tokens[i]:
                if (s not in punc and s != ' ') or s in excluded_str:
                    new_str += s
            tokens[i] = new_str
    return tokens


def message_split(message):
    #print(string.punctuation)
    punc = "!\"#$%&'()+,-/:;=?@.[\]^_`{|}~"
    #print(punc)
    #punc = re.sub("[*<>\.\-\/\\]", "", string.punctuation)
    splitters = "\s\\" + "\\".join(punc)
    #print(splitters)
    #splitters = "\\".join(punc)
    # splitter_regex = re.compile("([{}]+)".format(splitters))
    splitter_regex = re.compile("([{}])".format(splitters))
    tokens = re.split(splitter_regex, message)

    tokens = list(filter(lambda x: x != "", tokens))
    
    #print("tokens: ", tokens)
    tokens = post_process_tokens(tokens, punc)

    tokens = [
        token.strip()
        for token in tokens
        if token != "" and token != ' ' 
    ]
    tokens = [
        token
        for idx, token in enumerate(tokens)
        if not (token == "<*>" and idx > 0 and tokens[idx - 1] == "<*>")
    ]
    #print("tokens: ", tokens)
    #tokens = [token.strip() for token in message.split()]
    #print(tokens)
    return tokens


def tree_match(match_tree, log_list):
    # print("Worker {} start matching {} lines.".format(os.getpid(), len(log_list)))
    matched_results = []
    for log_content in log_list:
        # Full match
        if log_content in match_tree["$NO_STAR$"]:
            matched_results.append(log_content)
            continue

        log_tokens = message_split(log_content)
        template, parameter_str = match_template(match_tree, log_tokens)
        if template:
            matched_results.append(template)
        else:
            matched_results.append("NoMatch")
    return matched_results

def match_template(match_tree, log_tokens):
    result = []
    find_template(match_tree, log_tokens, result, [])
    if result:
        result.sort(key=lambda x: (-x[1][0], x[1][1]))
        return result[0][0], result[0][2]
    return None, None

def find_template(move_tree, log_tokens, result, parameter_list):
    if len(log_tokens) == 0:
        for key, value in move_tree.items():
            if isinstance(value, tuple):
                result.append((key, value, tuple(parameter_list)))
        if "<*>" in move_tree:
            parameter_list.append("")
            move_tree = move_tree["<*>"]
            for key, value in move_tree.items():
                if isinstance(value, tuple):
                    result.append((key, value, tuple(parameter_list)))
        return
    token = log_tokens[0]

    if token in move_tree:
        find_template(move_tree[token], log_tokens[1:], result, parameter_list)

    if "<*>" in move_tree:
        if isinstance(move_tree["<*>"], dict):
            next_keys = move_tree["<*>"].keys()
            next_continue_keys = []
            for nk in next_keys:
                nv = move_tree["<*>"][nk]
                if not isinstance(nv, tuple):
                    next_continue_keys.append(nk)

            idx = 0
            # print "Now>>>", log_tokens
            # print "next>>>", next_continue_keys
            while idx < len(log_tokens):
                token = log_tokens[idx]
                # print("try", token)
                if token in next_continue_keys:
                    # print("add", "".join(log_tokens[0:idx]))
                    parameter_list.append("".join(log_tokens[0:idx]))
                    # print("End at", idx, parameter_list)
                    find_template(move_tree["<*>"], log_tokens[idx:], result, parameter_list)
                    if parameter_list:
                        parameter_list.pop()
                    # print("back", parameter_list)
                idx += 1
            if idx == len(log_tokens):
                parameter_list.append("".join(log_tokens[0:idx]))
                find_template(move_tree["<*>"], log_tokens[idx+1:], result, parameter_list)
                if parameter_list:
                    parameter_list.pop()


def main():
    parser = argparse.ArgumentParser(description='Match log messages with templates.')
    parser.add_argument('--logs', type=str, required=True, help='Path to the file containing log messages.')
    parser.add_argument('--templates', type=str, required=True, help='Path to the file containing log templates.')
    
    args = parser.parse_args()

    with open(args.templates, 'r') as f:
        templates = [line.strip() for line in f.readlines()]

    tree = template_tree()
    tree.add_templates(templates)

    with open(args.logs, 'r') as f:
        logs = [line.strip() for line in f.readlines()]

    matched_templates = tree.match_event(logs)

    for i in range(len(logs)):
        print("{} -> {}".format(logs[i], matched_templates[i]))

if __name__ == "__main__":
    main()
    log_templates = [
        "generating core.<*>",
        "<*> double-hummer alignment exceptions",
        "<*> L3 <*> error(s) (dcr <*>) detected and corrected",
        "floating point instr. enabled.....<*>",
        "ciod: LOGIN chdir(<*>) failed: Permission denied",
        "size of DDR we are caching...............<*> (<*>)",
        "memory manager / command manager address parity..<*>",
        "this is a test <*> case",
        "this is a test demo case",
        "this is a test demo <*> case"
    ]
    template_tree = template_tree()
    template_tree.add_templates(log_templates)
    # print(template_tree.template_list)

    log_list = [
        "generated core.123",
        "generating core.456",
        "generating core.789",
        "1347195 double-hummer alignment exceptions",
        "7121867 L3 EDRAM error(s) (dcr 0x0157) detected and corrected",
        "floating point instr. enabled.....1",
        "ciod: LOGIN chdir(123) failed: Permission denied",
        "size of DDR we are caching...............123 (456)",
        "memory manager / command manager address parity..2393",
        "this is a test 123 case",
        "this is a test demo case",
        "this is a test demo 123 case"
    ]

    template_list = template_tree.match_event(log_list)
    for i in range(len(log_list)):
        print("{} -> {}".format(log_list[i], template_list[i]))