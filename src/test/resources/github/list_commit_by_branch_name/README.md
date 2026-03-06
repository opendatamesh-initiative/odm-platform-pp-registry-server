# Call this endpoint 
This endpoint must be called to retrive the list of commits of a specific branch:

curl --location 'https://api.github.com/repos/{owner}/{repo}/commits?sha=test' \
--header 'Authorization: Bearer {PAT}'

# Retrived this response
You can find the response at this location: src/test/resources/github/list_commit_by_branch_name/list_commit_by_branch_name.json