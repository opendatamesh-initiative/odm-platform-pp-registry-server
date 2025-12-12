package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Git organization
 */
public class Organization {
    private String id;
    private String name;
    private String url;
    private List<User> members;
    private List<Repository> repositories;

    public Organization() {
        this.members = new ArrayList<>();
        this.repositories = new ArrayList<>();
    }

    public Organization(String id, String name, String url) {
        this();
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public void addMember(User member) {
        if (member != null && !members.contains(member)) {
            members.add(member);
        }
    }

    public void addRepository(Repository repository) {
        if (repository != null && !repositories.contains(repository)) {
            repositories.add(repository);
        }
    }
}
