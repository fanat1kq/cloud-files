package ru.example.cloudfiles.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.example.cloudfiles.dto.ResourceType;
import ru.example.cloudfiles.dto.response.ResourceInfoResponseDTO;

import ru.example.cloudfiles.mapper.model.PathInfo;
import ru.example.cloudfiles.mapper.model.ResourceMappingData;
import ru.example.cloudfiles.util.FilePathUtils;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

          @Mapping(target = "type", expression = "java(toResourceType(resourceMappingData.isDirectory()))")
          ResourceInfoResponseDTO toResourceInfoDTO(ResourceMappingData resourceMappingData, PathInfo pathInfo);

          default ResourceType toResourceType(boolean isDirectory) {
                    return isDirectory ? ResourceType.DIRECTORY : ResourceType.FILE;
          }

          default PathInfo extractPathInfo(String relativePath, boolean isDirectory) {
                    return FilePathUtils.extractPathInfo(relativePath, isDirectory);
          }
}