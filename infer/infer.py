import pandas as pd
import sys
import json
import time
import os
import argparse
from utils.openai_request import make_openai_request

def parse_output(output):
    output = output.replace("```", '').replace("json", '')
    return json.loads(output)

def rca_loc(data, instruction, return_prompt, basic_instruction, separator, historical_issues, execution_paths, related_code):
    for index, row in data.iterrows():
        if os.path.exists(f'./rca_l_gpt4/{row["Bug ID"].strip()}.json'):
            continue
        
        print(row['Bug ID'])
        bug_id = row['Bug ID'].strip()
        demo_prompt = basic_instruction
        demo_prompt += f"{separator}Bug title of the bug: {row['Title'].strip()}"
        
        if not pd.isnull(row['Description']):
            demo_prompt += f"{separator}Bug description: {row['Description'].strip()}"
        
        demo_prompt += f"{separator}Logs and Stack Trace: {row['Logs and Stack Trace'].strip()}"
        
        with open(f'../data/rca_l_gt/{bug_id}.json', 'r') as f:
            data = json.load(f)
        
        demo_prompt += instruction
        demo_prompt += f"{separator}Components list: {str(data['return']['potential_root_cause'])}"
        
        # Add new parameters to the prompt
        demo_prompt += f"{separator}Historical Issues: {historical_issues}"
        demo_prompt += f"{separator}Execution Paths: {execution_paths}"
        demo_prompt += f"{separator}Related Code: {related_code}"
        
        demo_prompt += return_prompt
        
        ret = make_openai_request(demo_prompt, token_usage=True)
        ret = parse_output(ret)
        ret = json.dumps(ret, indent=4)
        
        with open(f'./rca_l_gpt4/{bug_id}.json', 'w') as f:
            f.write(ret)
        
        time.sleep(5)
        print("Return response: ", ret)
        print("Ground truth: ", data['return']['real_root_cause'])
        print("--------------------------------------------------------")

def rca_s(data, instruction, basic_instruction, separator, historical_issues, execution_paths, related_code):
    for index, row in data.iterrows():
        print(row['Bug ID'])
        if os.path.exists(f'./rca_l_gpt4/{row["Bug ID"].strip()}.txt'):
            continue
        
        demo_prompt = basic_instruction
        demo_prompt += f"{separator}Bug title of the bug: {row['Title'].strip()}"
        
        if not pd.isnull(row['Description']):
            demo_prompt += f"{separator}Bug description: {row['Description'].strip()}"
        
        bug_id = row['Bug ID'].strip()
        
        demo_prompt += f"{separator}Logs and Stack Trace: {row['Logs and Stack Trace'].strip()}"

        
        # Add new parameters to the prompt
        demo_prompt += f"{separator}Historical Issues: {historical_issues}"
        demo_prompt += f"{separator}Execution Paths: {execution_paths}"
        demo_prompt += f"{separator}Related Code: {related_code}"
        
        demo_prompt += instruction
        
        ret = make_openai_request(demo_prompt, "")
        
        with open(f'./gpt4/{row["Bug ID"].strip()}.txt', 'w') as f:
            f.write(ret)
        
        time.sleep(2)

def main():
    parser = argparse.ArgumentParser(description="Root Cause Analysis Script")
    parser.add_argument("issue_report", help="Path to the issue report file")
    parser.add_argument("--historical_issues", help="Historical issues for consideration in RCA")
    parser.add_argument("--execution_paths", help="Execution paths for consideration in RCA")
    parser.add_argument("--related_code", help="Related code for consideration in RCA")
    args = parser.parse_args()

    # Load issue report data
    data = pd.read_csv(args.issue_report)

    # Define prompts
    loc_prompt = """
    \n\n
    INSTRUCTION: Please localize the root cause components of above input for further issue solving. Please note that the above input is a bug report and component could be a class or other unit.
    Once identified, rank these components in descending order of their likelihood to be the root causes, based on the list of components provided.
    """

    return_prompt = """
    \n\n
    Return format should be pure json format which can be handled by json.loads() function in python:
     "return": {
     "potential_root_cause": ["ranked list of components"],
     }
    """

    s_prompt = """
    \n\n\n
    INSTRUCTION: Please infer, analyze and summarize the root cause of above input for further issue solving. Please note that the above input is a bug report with corresponding logs and stack traces.
    The summarized root cause should be about 80-100 words, no more than 120 words, and should cover important information as much as possible. Just return the summarized root cause without any additional output.
    """

    separator = '\n\nHere is the '
    basic_instruction = "Read the following bug report and summarize the root cause."

    # Run RCA processes
    rca_loc(data, loc_prompt, return_prompt, basic_instruction, separator, 
            args.historical_issues, args.execution_paths, args.related_code)
    rca_s(data, s_prompt, basic_instruction, separator, 
          args.historical_issues, args.execution_paths, args.related_code)

if __name__ == "__main__":
    main()