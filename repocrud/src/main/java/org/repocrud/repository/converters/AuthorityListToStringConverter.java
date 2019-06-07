package org.repocrud.repository.converters;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by study on 11/15/14.
 */
@Converter(autoApply = true)
public class AuthorityListToStringConverter implements AttributeConverter<List<GrantedAuthority>, String> {


    @Override
    public String convertToDatabaseColumn(List<GrantedAuthority> grantedAuthorities) {

        return ( grantedAuthorities == null) ? null: grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(";"));
    }

    @Override
    public List<GrantedAuthority> convertToEntityAttribute(String s) {
        return s== null ?  null : Stream.of(s.split(";")).map(a-> new SimpleGrantedAuthority(s)).collect(Collectors.toList());

    }
}