#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
 
 void runAs(int gid, int uid, char **env, char *command) {
  int status = 0;
   int child = fork();
   if (child == 0) {
    setgid(gid);
     setuid(uid);
     do {
       putenv(*env);
       env++;
     } while (env != 0);
     execl ("/bin/ls", "ls", NULL);
   } else if (child > 0) {
   printf("child %d \n", child);
    waitpid(child, &status, 0);
   } else {
     // Error: fork() failed!
     printf("error");
   }
 }


 int main(int argc, char *argv[] )
 {
     char *env[3];
    // if ( argc != 2) {
     //    printf("usage: %s command\n",argv[0]);
   //      exit(1);
    // }
    /* env[0] = "HOME=/home/caroline";
     env[1] = "DISPLAY=:0";
     env[2] = NULL;
     runAs(100, 1000, env, argv[1]);*/

     env[0] = "HOME=/home/cx";
     env[1] = "DISPLAY=:1";
     //runAs(1001, 1001, env, argv[1]);
     runAs(1001, 1001, env, "java -Dspring.profiles.active=local -jar target/server-0.0.1-SNAPSHOT.jar");
     return 0;
 }
