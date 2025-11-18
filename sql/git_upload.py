import os
import subprocess
import shutil

def add_files_to_github_repo(repo_path, files):
    # Create a temporary folder to clone into
    local_repo = "/tmp/repo"

    # Clone the repo (only once)
    subprocess.run(["git", "clone", repo_path, local_repo], check=True)

    # Move into cloned repo
    os.chdir(local_repo)

    # Copy your file(s) into the repo
    shutil.copy(files, local_repo)

    # Stage, commit, and push changes
    subprocess.run(["git", "add", "."], check=True)
    subprocess.run(["git", "commit", "-m", "Added new files"], check=True)
    subprocess.run(["git", "push"], check=True)

    return "Files added and pushed successfully!"
