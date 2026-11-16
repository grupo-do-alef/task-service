package com.github.ggarabs.task_service.adapters.inbound;

import com.github.ggarabs.task_service.adapters.outbound.TaskEntity;
import com.github.ggarabs.task_service.adapters.outbound.TaskStatus;
import com.github.ggarabs.task_service.application.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskEntity> createTask(@RequestBody TaskEntity task, Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            try {
                task.setIdemployer(UUID.fromString(authentication.getName()));
            } catch (IllegalArgumentException e) {
                task.setIdemployer(UUID.randomUUID());
            }
        } else {
            if (task.getIdemployer() == null) {
                task.setIdemployer(UUID.randomUUID());
            }
        }

        TaskEntity created = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TaskEntity>> getAllTasks(@RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByStatus(status));
        }
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> getTaskById(@PathVariable UUID id) {
        TaskEntity task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskEntity> updateStatus(
            @PathVariable UUID id,
            @RequestParam TaskStatus status,
            Authentication authentication) {
        if (authentication != null) {
            requireOwnerOrAdmin(id, authentication);
        }
        TaskEntity updated = taskService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, Authentication authentication) {
        if (authentication != null) {
            requireOwnerOrAdmin(id, authentication);
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private void requireOwnerOrAdmin(UUID taskId, Authentication authentication) {
        if (authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRATOR"::equals))
            return;
        TaskEntity task = taskService.getTaskById(taskId);
        if (!UUID.fromString(authentication.getName()).equals(task.getIdemployer())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A vaga pertence a outro contratante");
        }
    }
}