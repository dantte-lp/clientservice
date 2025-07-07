package com.bank.clientservice.mapper;

import com.bank.clientservice.dto.ClientDto;
import com.bank.clientservice.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientDto toDto(Client client);

    @Mapping(target = "clientNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(ClientDto dto);

    List<ClientDto> toDtoList(List<Client> clients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ClientDto dto, @MappingTarget Client client);
}