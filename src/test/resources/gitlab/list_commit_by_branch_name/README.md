# Call this endpoint 
This endpoint must be called to retrive the list of commits of a specific branch:

curl --location 'https://gitlab.com/api/v4/projects/{projectId}/repository/commits?ref_name=test' \
--header 'PRIVATE-TOKEN: {PAT}' 

# Retrived this response
You can find the response at this location: src/test/resources/gitlab/list_commit_by_branch_name/list_commit_by_branch_name.json