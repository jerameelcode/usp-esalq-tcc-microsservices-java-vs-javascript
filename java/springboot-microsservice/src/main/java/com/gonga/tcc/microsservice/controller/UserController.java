package com.gonga.tcc.microsservice.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gonga.tcc.microsservice.dtos.UserRequestDTO;
import com.gonga.tcc.microsservice.dtos.UserResponseDTO;
import com.gonga.tcc.microsservice.service.UserService;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PagedResourcesAssembler<UserResponseDTO> pagedResourcesAssembler;

    private static final String ALL_USERS_REL = "all-users";

    public UserController(UserService userService, PagedResourcesAssembler<UserResponseDTO> pagedResourcesAssembler) {
        this.userService = userService;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<UserResponseDTO>>> getAllUsers(@PageableDefault Pageable pageable) {
        var usersPage = userService.findAll(pageable);
        var pagedModel = pagedResourcesAssembler.toModel(usersPage);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> getUserById(@PathVariable UUID id) {
        var user = userService.findById(id);
        var entityModel = EntityModel.of(user,
            linkTo(methodOn(UserController.class).getAllUsers(null)).withRel(ALL_USERS_REL),
            linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel()
        );
        return ResponseEntity.ok(entityModel);
    }

    @PostMapping
    public ResponseEntity<EntityModel<UserResponseDTO>> createUser(@RequestBody UserRequestDTO userRequestDTO) {
        var createdUser = userService.createUser(userRequestDTO);
        var entityModel = EntityModel.of(createdUser,
            linkTo(methodOn(UserController.class).getAllUsers(null)).withRel(ALL_USERS_REL),
            linkTo(methodOn(UserController.class).createUser(null)).withSelfRel()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO userRequestDTO) {
        var updatedUser = userService.updateUser(id, userRequestDTO);
        var entityModel = EntityModel.of(updatedUser,
            linkTo(methodOn(UserController.class).getAllUsers(null)).withRel(ALL_USERS_REL),
            linkTo(methodOn(UserController.class).updateUser(id, userRequestDTO)).withSelfRel()
        );
        return ResponseEntity.ok(entityModel);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDTO>> partialUpdateUser(@PathVariable UUID id, @RequestBody Map<String, Object> updates) {
        var updatedUser = userService.partialUpdateUser(id, updates);
        var entityModel = EntityModel.of(updatedUser,
            linkTo(methodOn(UserController.class).getAllUsers(null)).withRel(ALL_USERS_REL),
            linkTo(methodOn(UserController.class).partialUpdateUser(id, null)).withSelfRel()
        );
        return ResponseEntity.ok(entityModel);
    }
}
