package com.inventory.inventorymanagementsystem.security;



import com.inventory.inventorymanagementsystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    @Autowired
    private User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        String roleName = "ROLE_" + user.getRole().getRoleName();
//        return List.of(new SimpleGrantedAuthority(roleName));
//    }
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().name()));
}


    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}


