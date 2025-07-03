package com.example.document;

// Spring Data MongoDB imports
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// Java standard library imports
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a platform.
 */
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

    /** Gets the MongoDB document ID. */
    public String getId() {
        return id;
    }

    /** Sets the MongoDB document ID. */
    public void setId(String id) {
        this.id = id;
    }

    /** Gets the platform name. */
    public String getName() {
        return name;
    }

    /** Sets the platform name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Gets the embedded courses in the platform. */
    public List<CourseEmbed> getCourses() {
        return courses;
    }

    /** Sets the embedded courses in the platform. */
    public void setCourses(List<CourseEmbed> courses) {
        this.courses = courses;
    }

    /**
     * Embedded course document within a platform.
     */
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

        /** Gets the course ID. */
        public String getId() {
            return id;
        }

        /** Sets the course ID. */
        public void setId(String id) {
            this.id = id;
        }

        /** Gets the course title. */
        public String getTitle() {
            return title;
        }

        /** Sets the course title. */
        public void setTitle(String title) {
            this.title = title;
        }

        /** Gets the list of enrolled users in the course. */
        public List<UserEmbed> getEnrolledUsers() {
            return enrolledUsers;
        }

        /** Sets the list of enrolled users in the course. */
        public void setEnrolledUsers(List<UserEmbed> enrolledUsers) {
            this.enrolledUsers = enrolledUsers;
        }
    }

    /**
     * Embedded user document within a course.
     */
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

        /** Gets the user ID. */
        public String getId() {
            return id;
        }

        /** Sets the user ID. */
        public void setId(String id) {
            this.id = id;
        }

        /** Gets the user name. */
        public String getName() {
            return name;
        }

        /** Sets the user name. */
        public void setName(String name) {
            this.name = name;
        }

        /** Gets the user email. */
        public String getEmail() {
            return email;
        }

        /** Sets the user email. */
        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * Aggregates all users enrolled in any course of this platform.
     * @return list of enrolled users
     */
    public List<UserEmbed> getUsers() {
        List<UserEmbed> users = new ArrayList<>();
        if (courses != null) {
            for (CourseEmbed course : courses) {
                if (course.getEnrolledUsers() != null) {
                    users.addAll(course.getEnrolledUsers());
                }
            }
        }
        return users;
    }
}
