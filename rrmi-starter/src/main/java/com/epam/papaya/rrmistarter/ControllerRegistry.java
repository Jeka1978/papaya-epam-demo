package com.epam.papaya.rrmistarter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeny Borisov
 */
@RestController
@RequestMapping(ControllerRegistry.GENERATED_CONTROLLER_PREFIX)
public class ControllerRegistry {
    public static final String GENERATED_CONTROLLER_PREFIX = "/api/generated/";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GenericApplicationContext context;

    private Map<String, Pair<Method, AutoGeneratedController>> map = new HashMap<>();

    @GetMapping("{methodId}")
    @SneakyThrows
    public Object restInvocation(@PathVariable String methodId){
        Method method = map.get(methodId)._1();
        AutoGeneratedController adapter = map.get(methodId)._2();
        return method.invoke(adapter);
    }

    @PostMapping("{methodId}")
    @SneakyThrows
    public Object restInvocation(@PathVariable String methodId,@RequestBody String body){
        Pair<Method, AutoGeneratedController> pair = map.get(methodId);
        if (pair == null) {
            throw new UnsupportedOperationException("remote method " + methodId + " was not registered in controller");
        }
        Method method = pair._1();
        AutoGeneratedController adapter = pair._2();
        Object o = objectMapper.readValue(body, method.getParameterTypes()[0]);
        return method.invoke(adapter,o);
    }


    @EventListener(ContextRefreshedEvent.class)
    public void registerControllers() {
        System.out.println("endpoints mapping started");
        Collection<AutoGeneratedController> adapters = context.getBeansOfType(AutoGeneratedController.class).values();
        for (AutoGeneratedController adapter : adapters) {
            for (Method method : adapter.getClass().getDeclaredMethods()) {
                map.put(method.getName(), new Pair<>(method, adapter));
            }

        }
    }

}
