package com.parent.ws.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.parent.ws.app.constants.ErrorMessages;
import com.parent.ws.app.exceptions.UserServiceException;
import com.parent.ws.app.persistence.entities.UserEntity;
import com.parent.ws.app.persistence.repositories.UserRepository;
import com.parent.ws.app.service.protocols.UserService;
import com.parent.ws.app.shared.Utils;
import com.parent.ws.app.shared.dto.AddressDto;
import com.parent.ws.app.shared.dto.UserDto;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    private static final int PUBLIC_ID_LENGHT = 30;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Utils utils;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDto createUser(UserDto userDto) {
        String userEmail = userDto.getEmail();
        String userPlainTextPassword = userDto.getPassword();

        UserEntity record = userRepository.findByEmail(userEmail);
        if (record != null) {
            throw new RuntimeException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());
        }

        ModelMapper mapper = new ModelMapper();

        String userPublicId = utils.generatePublicId(PUBLIC_ID_LENGHT);
        String userEncryptedPassword = bCryptPasswordEncoder.encode(userPlainTextPassword);

        List<AddressDto> addressDtoList = userDto.getAddresses();
        for (int i = 0; i < addressDtoList.size(); i++) {
            AddressDto address = userDto.getAddresses().get(i);
            address.setUserDetails(userDto);
            address.setAddressId(utils.generatePublicId(PUBLIC_ID_LENGHT));
            userDto.getAddresses().set(i, address);
        }

        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setUserId(userPublicId);
        userEntity.setEncryptedPassword(userEncryptedPassword);

        UserEntity persistedUserDetails = userRepository.save(userEntity);
        UserDto userDetailsDto = mapper.map(persistedUserDetails, UserDto.class);

        return userDetailsDto;
    }

    @Override
    public UserDto updateUser(String userId, UserDto userDto) {
        UserEntity userEntity = getUserEntityById(userId);

        String firsName = userDto.getFirstName();
        String lastName = userDto.getLastName();
        userEntity.setFirstName(firsName);
        userEntity.setLastName(lastName);

        UserEntity updatedUserDetails = userRepository.save(userEntity);

        UserDto updatedUserDetailsDto = new UserDto();
        BeanUtils.copyProperties(updatedUserDetails, updatedUserDetailsDto);

        return updatedUserDetailsDto;
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = getUserEntityById(userId);
        userRepository.delete(userEntity);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = getUserEntityById(userId);
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(userEntity, userDto);

        return userDto;

    }

    public UserEntity getUserEntityById(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) {
            throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        return userEntity;
    }

    @Override
    public List<UserDto> getUsers(int page, int limit) {
        List<UserDto> userDtoList = new ArrayList<UserDto>();
        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> userPage = userRepository.findAll(pageableRequest);
        List<UserEntity> userEntityList = userPage.getContent();

        userEntityList.forEach(userEntity -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(userEntity, userDto);
            userDtoList.add(userDto);
        });

        return userDtoList;
    }

    @Override
    public UserDto getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        UserDto userDetailsDto = new UserDto();
        BeanUtils.copyProperties(userEntity, userDetailsDto);

        return userDetailsDto;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }

        String userEmail = userEntity.getEmail();
        String userEncryptedPassword = userEntity.getEncryptedPassword();
        UserDetails userDetails = new User(userEmail, userEncryptedPassword, new ArrayList<>());

        return userDetails;
    }

}
