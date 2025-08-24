package com.profile.candidate.dto;

import java.time.LocalDate;
import java.util.Set;

public class UserDetailsDTO {

        private String userId;
        private String userName;
        private String password;
        private String confirmPassword;
        private String email;
        private String personalemail;
        private String phoneNumber;
        private String dob;
        private String gender;
        private LocalDate joiningDate;
        private String designation;
        private Set<String> roles;  // keeping as Set<String> per your request
        private String status;
        private String entity;

        public String getUserId() {
                return userId;
        }

        public void setUserId(String userId) {
                this.userId = userId;
        }

        public String getUserName() {
                return userName;
        }

        public void setUserName(String userName) {
                this.userName = userName;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public String getConfirmPassword() {
                return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
                this.confirmPassword = confirmPassword;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPersonalemail() {
                return personalemail;
        }

        public void setPersonalemail(String personalemail) {
                this.personalemail = personalemail;
        }

        public String getPhoneNumber() {
                return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
        }

        public String getDob() {
                return dob;
        }

        public void setDob(String dob) {
                this.dob = dob;
        }

        public String getGender() {
                return gender;
        }

        public void setGender(String gender) {
                this.gender = gender;
        }

        public LocalDate getJoiningDate() {
                return joiningDate;
        }

        public void setJoiningDate(LocalDate joiningDate) {
                this.joiningDate = joiningDate;
        }

        public String getDesignation() {
                return designation;
        }

        public void setDesignation(String designation) {
                this.designation = designation;
        }

        public Set<String> getRoles() {
                return roles;
        }

        public void setRoles(Set<String> roles) {
                this.roles = roles;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }

        public String getEntity() {
                return entity;
        }

        public void setEntity(String entity) {
                this.entity = entity;
        }
}
