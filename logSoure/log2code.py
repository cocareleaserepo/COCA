import pandas as pd
import re
import json

data = pd.read_excel('./data/D-bug-full.xlsx')
level_list = ['ERROR', 'WARN', 'INFO', 'DEBUG' ,'FATAL', 'TRACE']
trace_indicator = "at org.apache" # we just ignore all java.util related traces, because they are mostly trained by LLMs
# In this scenario, "at org.apache" indicates our own project
log_trace_dict = {}

for i in range(len(data)):
    bug_id = data.loc[i, 'Bug ID']
    temp_dict = {}
    log_dict = {}
    trace_dict = {}
    logs = data.loc[i, 'Logs and Stack Trace']
    # delete the blank lines in logs
    logs = re.sub(r'\n\s*\n', '\n', logs)
    for line, line_number in zip(logs.split('\n'), range(len(logs.split('\n')))):
        if trace_indicator in line:
            trace_dict[line_number] = line
        else:
            for level in level_list:
                if level in line:
                    log_dict[line_number] = line
    log_trace_dict[bug_id] = {'logs': log_dict, 'traces': trace_dict}



def parse_stack_trace_line(stack_trace_line): 
    stack_trace_line = stack_trace_line.strip().lstrip("at ").rstrip(")")
    parsed_info = {}  
    try:  
        full_class_method, location = stack_trace_line.split("(")  
        full_class, method = full_class_method.rsplit(".", 1)  
        location = location.replace(")", "") 
        file_name, line_number = location.split(":")  
        line_number = int(line_number) 
  
        parsed_info['package_class'] = full_class  
        parsed_info['method'] = method  
        parsed_info['file_name'] = file_name  
        parsed_info['line_number'] = line_number  
    except Exception as e:  
        print(f"Error parsing stack trace line: {str(e)}")  
        return None  
  
    return parsed_info


import os
for bug_id, log_trace in log_trace_dict.items():
    print(f"-----------------{bug_id}-----------------")
    parsed_logs = {}
    prased_traces = {} 
    if os.path.exists(f"./data/parsed_logs_traces/{bug_id}.json"):
        continue
    for line_number, trace in log_trace['traces'].items():
        parsed_trace = parse_stack_trace_line(trace)
        if parsed_trace:
            prased_traces[line_number] = parsed_trace
    log_trace['parsed_traces'] = prased_traces
    bug_id = bug_id.strip()
    with open(f"./data/parsed_logs_traces/{bug_id}.json", "w") as f:
        json.dump(log_trace, f,)