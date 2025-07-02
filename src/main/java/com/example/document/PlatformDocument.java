package com.example.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "platforms")
public class PlatformDocument {

    @Id
    private String id;

    private String name;

    private List<CourseEmbed> courses;

    public PlatformDocument() {}

    public PlatformDocument(String name, List<CourseEmbed> courses) {
        this.name = name;
        this.courses = courses;
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

    public List<CourseEmbed> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseEmbed> courses) {
        this.courses = courses;
    }

    public static class CourseEmbed {
        private String id;
        private String title;
        private List<UserEmbed> enrolledUsers;

        public CourseEmbed() {}

        public CourseEmbed(String id, String title, List<UserEmbed> enrolledUsers) {
            this.id = id;
            this.title = title;
            this.enrolledUsers = enrolledUsers;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<UserEmbed> getEnrolledUsers() {
            return enrolledUsers;
        }

        public void setEnrolledUsers(List<UserEmbed> enrolledUsers) {
            this.enrolledUsers = enrolledUsers;
        }
    }

    public static class UserEmbed {
        private String id;
        private String name;
        private String email;

        public UserEmbed() {}

        public UserEmbed(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public List<UserEmbed> getUsers() {
        List<UserEmbed> users = new ArrayList<>();
        if (courses != null) {
            for (PlatformDocument.CourseEmbed course : courses) {
                if (course.getEnrolledUsers() != null) {
                    users.addAll(course.getEnrolledUsers());
                }
            }
        }
        return users;
    }

}
