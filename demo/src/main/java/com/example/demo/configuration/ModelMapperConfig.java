package com.example.demo.configuration;

import com.example.demo.dto.response.MessageDTO;
import com.example.demo.entity.Message;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // DẠY ModelMapper cách map từ Message (Entity) sang MessageDTO
        modelMapper.createTypeMap(Message.class, MessageDTO.class)
                // Lấy source từ message.getSender().getId()
                // và gán vào destination là dto.setSenderId()
                .addMapping(src -> src.getSender().getId(), MessageDTO::setSenderId)

                // Tương tự cho chatRoomId
                .addMapping(src -> src.getChatRoom().getId(), MessageDTO::setChatRoomId);

        return modelMapper;
    }
}