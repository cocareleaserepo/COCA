import signal
import time
import openai
import os
import re
import time
import string
from tqdm import tqdm
import http.client
import json

os.environ["OPENAI_API_BASE"] = ""

def get_llama3_request(instruction, initial_instruction = "You are a helpful assistant good at understand system bugs."):
    client = openai.OpenAI(
    api_key="",
    base_url="",
)   
    chat_completion = client.chat.completions.create(
    model="meta-llama/Meta-Llama-3.1-405B-Instruct",
    messages=[{"role": "system", "content": initial_instruction},
                {"role": "user", "content": instruction},],)
    return chat_completion.choices[0].message.content



def get_embedding(text, key, model="text-embedding-3-large"):
   client = openai.OpenAI(
       api_key=key,
    base_url="",
    )
   text = text.replace("\n", " ")
   return client.embeddings.create(input = [text], model=model).data[0].embedding

def make_openai_request(instruction, key, model='gpt-4o', n=1, temperature=0, max_tokens=4096, initial_instruction = "You are a helpful assistant good at understand system bugs."):
    messages = [{"role": "system", "content": initial_instruction},
                {"role": "user", "content": instruction},
        ]
    
    retry_times = 0
    client = openai.OpenAI(
    api_key=key,
    base_url="",
)

    while retry_times < 3:
        answers = client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=temperature,
            max_tokens=max_tokens,
            n = n,
            frequency_penalty=0.0,
            presence_penalty=0.0,
            #stop=None,
        )
        return answers.choices[0].message.content



if __name__ == "__main__":
    key = ""
 