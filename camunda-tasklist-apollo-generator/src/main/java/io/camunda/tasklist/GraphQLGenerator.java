package io.camunda.tasklist;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.apollographql.apollo3.compiler.ApolloCompiler;
import com.apollographql.apollo3.compiler.Options;
import com.apollographql.apollo3.compiler.TargetLanguage;

public class GraphQLGenerator {

    public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException {
        Set<File> executables = new HashSet<>();
        if (args.length>2) {
            for(int i=2;i<args.length;i++) {
                executables.add(new File(args[i]));
            }
        }
        
        Options options = new Options(
                executables,
              new File(args[0]),
              new File(args[1]),
              new File(args[1]),
              "io.camunda.tasklist.client"
          );
        setOption(options, "targetLanguage", TargetLanguage.JAVA);

        ApolloCompiler.INSTANCE.write(options);

    }

    
    private static void setOption(Options options, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = Options.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(options, value);
    }
}
