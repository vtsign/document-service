package tech.vtsign.documentservice.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.vtsign.documentservice.model.LoginServerResponseDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class UserDetailsImpl implements UserDetails {
    private LoginServerResponseDto loginServerResponseDto;

    public UserDetailsImpl(LoginServerResponseDto loginServerResponseDto) {
        this.loginServerResponseDto = loginServerResponseDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        //list of permission
        if(this.loginServerResponseDto.getPermissions()!=null)
            this.loginServerResponseDto.getPermissions().forEach(permission ->{
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getName());
                list.add(grantedAuthority);
            });
        // list of roles
        if(this.loginServerResponseDto.getRoles()!=null)
            this.loginServerResponseDto.getRoles().forEach(role ->{
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_"+role.getName());
                list.add(grantedAuthority);
            });
        return list;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return loginServerResponseDto.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !loginServerResponseDto.isBlocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return loginServerResponseDto.isEnabled();
    }
}
