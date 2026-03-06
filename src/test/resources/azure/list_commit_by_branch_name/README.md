# Call this endpoint 
This endpoint must be called to retrive the list of commits of a specific branch:

curl --location 'https://dev.azure.com/{organization}/{project}/_apis/git/repositories/{repositoryId}/commits?api-version=7.1&$itemVersion.version=test&$itemVersion.versionType=branch' \
--header 'Authentication: Basic: {PAT}' \
--header 'Authorization: Basic: {username:PAT}'

# Retrived this response
You can find the response at this location: src/test/resources/azure/list_commit_by_branch_name/list_commit_by_branch_name.json