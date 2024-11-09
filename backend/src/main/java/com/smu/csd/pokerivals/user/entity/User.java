package com.smu.csd.pokerivals.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.util.Date;

/**
 * Representation of system user (can be type {@link User} or {@link Admin}
 * Implements {@link Persistable<String>} to prevent saving of user with the same username twice
 */
@Entity
@Table(name="users") 
@Inheritance(strategy = InheritanceType.JOINED) // experiment with different kinds
@Getter
public abstract class User implements Persistable<String> {
        public User(){}

        @Id
        @Column(length = 100)
        @NotEmpty
        @Size(max =100)
        protected String username;

        @Column(columnDefinition = "TEXT")
        protected String description;

        @Column(length = 30, unique=true)
        @JsonIgnore
        private String googleSub;

        @Temporal(TemporalType.TIMESTAMP)
        @JsonIgnore
        private Date googleLinkTime;

        /**
         * Set the Google Subject Identifier (unique for each google account) per account.
         *
         * @param emailCreatedTime must be different from the one in the object
         * @param googleSub google subject identifier
         */
        public void updateGoogleSub(Date emailCreatedTime, String googleSub){
                if (!emailCreatedTime.equals(googleLinkTime)){
                        this.googleSub = googleSub;
                        this.googleLinkTime = emailCreatedTime;
                }
        }

        @Setter
        @Email
        private String email;

        protected User(String username,String googleSub){
                this.username = username;
                this.googleSub = googleSub;
        }

        @Setter
        @Getter
        @Transient
        private boolean update;

        @Override
        public boolean isNew() {
                return !this.update;
        }

        @PrePersist
        @PostLoad
        void markUpdated() {
                this.update = true;
        }

        @Override
        public String getId(){
                return username;
        }

        @Override
        public String toString() {
                return "User{" +
                        "googleSub='" + googleSub + '\'' +
                        ", username='" + username + '\'' +
                        '}';
        }
}