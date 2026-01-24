package com.chauhan.linkedInProject.user_service.service;

import com.chauhan.linkedInProject.user_service.dto.LoginRequestDto;
import com.chauhan.linkedInProject.user_service.dto.SignupRequestDto;
import com.chauhan.linkedInProject.user_service.dto.UserDto;
import com.chauhan.linkedInProject.user_service.entity.User;
import com.chauhan.linkedInProject.user_service.event.UserCreatedEvent;
import com.chauhan.linkedInProject.user_service.exception.BadRequestException;
import com.chauhan.linkedInProject.user_service.repository.UserRepository;
import com.chauhan.linkedInProject.user_service.utils.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final KafkaTemplate<Long, UserCreatedEvent> userCreatedEventKafkaTemplate;

    public UserDto signUp(SignupRequestDto signupRequestDto) {
        log.info("Signup a user with email: {}", signupRequestDto.getEmail());

        boolean exists = userRepository.existsByEmail(signupRequestDto.getEmail());
        if(exists) {
            throw new BadRequestException("User already exists");
        }

        User user = modelMapper.map(signupRequestDto, User.class);
        user.setPassword(BCrypt.hash(signupRequestDto.getPassword()));

        user = userRepository.save(user);

        //also notify to connection-service that a new user has created
        //setting the consumer
        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                .userId(user.getId())
                .name(user.getName())
                .build();

        userCreatedEventKafkaTemplate.send("user_created_topic", userCreatedEvent);

        return modelMapper.map(user, UserDto.class);
    }

    public String login(LoginRequestDto loginRequestDto) {
        log.info("Login request for user with email: {}", loginRequestDto.getEmail());

        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new BadRequestException(
                "Incorrect email or password"));// incorrect email -> user not found with this email id
        //doing like this [Incorrect email or password] bc to not give too much info abt system
        boolean isPasswordMatch = BCrypt.match(loginRequestDto.getPassword(), user.getPassword());

        if(!isPasswordMatch) {
            throw new BadRequestException("Incorrect email or password");//incorrect password
        }

        return jwtService.generateAccessToken(user);
    }
}
