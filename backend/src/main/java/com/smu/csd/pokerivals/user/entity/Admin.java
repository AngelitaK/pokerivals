package com.smu.csd.pokerivals.user.entity;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Admin user
 */
@Entity
@Table(name="admins")
public class Admin extends User {
    public Admin(){}

    public Admin(String username, String googleId) {
        super(username, googleId);
    }

    @Setter
    @Getter
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date activeSince;

    @JsonIgnore
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    private Admin invitedBy;

    @JsonIgnore
    @Getter
    @OneToMany(mappedBy = "invitedBy")
    private Set<Admin> invitees = new HashSet<>();

    /**
     * Admin can invite another admin - this method is to set another admin to be invited by this
     * @param a the admin being invited
     */
    public void addInvitee(Admin a){
        this.invitees.add(a);
        a.invitedBy = this;
    }

    /**
     * Check whether the admin has been activated
     * @return
     */
    @JsonGetter("active")
    public boolean isActive(){
        return getGoogleLinkTime() != null;
    }
}


