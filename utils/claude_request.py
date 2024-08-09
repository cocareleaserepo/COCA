import anthropic
# Create an instance of the Anthropics API client
def make_claude_request(text):
    client = anthropic.Anthropic(api_key='')

    response = client.messages.create(
        model="claude-3-5-sonnet-20240620",
        max_tokens=4096,
        system="You are a helpful assistant at root cause analysis.",
        messages=[{"role": "user", "content": text}]
    )
    return response.content[0].text

if __name__ == "__main__":
    question = "Who are you?"
    response = make_claude_request(question)