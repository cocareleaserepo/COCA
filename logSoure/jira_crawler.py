import requests  
from bs4 import BeautifulSoup  
  
class JiraCrawler:  
    def __init__(self, base_url='https://issues.apache.org/jira/browse'):  
        self.base_url = base_url  
  
    def fetch_issue_data(self, issue_id):  
        url = f"{self.base_url}/{issue_id}"  
        response = requests.get(url)  
        if response.status_code == 200:  
            return self.parse_issue_page(response.text)  
        else:  
            print(f"Failed to retrieve data for issue {issue_id}. Status code: {response.status_code}")  
            return None  
  
    def parse_issue_page(self, html_content):  
        soup = BeautifulSoup(html_content, 'html.parser')  
        issue_data = {}  
          
        title_section = soup.find('h1', id='summary-val')  
        issue_data['title'] = title_section.text.strip() if title_section else 'No title found'  
  
        description_section = soup.find('div', id='description-val')  
        issue_data['description'] = description_section.text.strip() if description_section else 'No description provided'  
  
        status_section = soup.find('span', id='status-val')  
        issue_data['status'] = status_section.text.strip() if status_section else 'No status available'  
  
        assignee_section = soup.find('span', id='assignee-val')  
        issue_data['assignee'] = assignee_section.text.strip() if assignee_section else 'No assignee'

        versions_val_section = soup.find('span', id='versions-val')  
        if versions_val_section:  
            version_text = versions_val_section.find('span', title=True).text.strip()  
            issue_data['versions'] = version_text  
        else:  
            issue_data['versions'] = 'No versions specified'   

        return issue_data  
  
if __name__ == "__main__":  
    crawler = JiraCrawler()  
    issue_id = 'HBASE-9593'  
    issue_data = crawler.fetch_issue_data(issue_id)  
    print(issue_data)  
