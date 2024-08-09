import requests  
import os  
  
class SCR_Crawler:  
    def __init__(self, download_dir='downloads', repo_name='apache/hbase'):  
        self.download_dir = download_dir  
        self.repo_name = repo_name
        self.system_name = repo_name.split('/')[1]
        os.makedirs(download_dir, exist_ok=True)  
  
    def download_source_code(self, version):  
        base_url = f"https://github.com/{self.repo_name}/archive/refs/tags/"  
        file_name = f"rel/{version}.tar.gz"
        url = f"{base_url}{file_name}"  
  
        response = requests.get(url)  
        if response.status_code == 200:  
            file_path = os.path.join(self.download_dir, f"{self.system_name}-{version}.tar.gz")  
            with open(file_path, 'wb') as f:  
                f.write(response.content)  
            print(f"Downloaded {file_name} to {file_path}")
            # unzip the downloaded file
            print(f"Unzipping {file_name} to {self.download_dir}")
            os.system(f"tar -xvf {file_path} -C {self.download_dir}")
            return file_path  
        else:  
            print(f"Failed to download {file_name}. Status code: {response.status_code}")  
            return None  
  
if __name__ == "__main__":  
    crawler = SCR_Crawler(repo_name='apache/hbase')  
    version = "0.94.11" 
    if version:  
        crawler.download_source_code(version)  
