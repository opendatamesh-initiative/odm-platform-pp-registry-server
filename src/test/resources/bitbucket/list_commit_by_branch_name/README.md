# Call this endpoint 
This endpoint must be called to retrive the list of commits of a specific branch:

curl --location 'https://api.bitbucket.org/2.0/repositories/{workspace}/{repo_slug}/commits/?include=test' \
--header 'Authorization: Bearer {PAT}'

# Retrived this response
You can find the response at this location: src/test/resources/bitbucket/list_commit_by_branch_name/list_commit_by_branch_name.json