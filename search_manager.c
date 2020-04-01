#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <unistd.h>
#include <signal.h>
#include <semaphore.h>
#include "longest_word_search.h"
#include "queue_ids.h"

char **statusArray;
int gNumPassages = 0;
int gNumPrefix = 0;
sem_t gPrefix;
sem_t gPassage;

size_t                  /* O - Length of string */
strlcpy(char       *dst,        /* O - Destination string */
        const char *src,      /* I - Source string */
        size_t      size)     /* I - Size of destination string buffer */
{
    size_t    srclen;         /* Length of source string */


    /*
     * Figure out how much room is needed...
     */

    size --;

    srclen = strlen(src);

    /*
     * Copy the appropriate amount...
     */

    if (srclen > size)
        srclen = size;

    memcpy(dst, src, srclen);
    dst[srclen] = '\0';

    return (srclen);
}

void catchSignal(int sigNum){
//    if (sigNum == SIGINT){

      int sigintCurrentPrefixCount;
      int sigintCurrentPassageCount;
      sem_getvalue(&gPrefix, &sigintCurrentPrefixCount);
      sem_getvalue(&gPassage, &sigintCurrentPassageCount);

      for(int i = 0; i < gNumPrefix;i++){
        if (sigintCurrentPrefixCount == 0){
          fprintf(stdout,"%s - pending\n" ,statusArray[i]);
        }
        else if(i == sigintCurrentPrefixCount ){
          fprintf(stdout,"%s - %d out of %d\n" ,statusArray[i],sigintCurrentPassageCount,gNumPassages);
        }
        else if(i  > sigintCurrentPrefixCount){
          fprintf(stdout,"%s - pending\n" ,statusArray[i]);
        }
        else{
          fprintf(stdout,"%s - done\n" ,statusArray[i]);
        }
      }
}

response_buf getResponse(int msqid){
// msgrcv to receive message
response_buf rbuf;
        int ret;
        do {
          //printf("receiving message \n");
          ret = msgrcv(msqid, &rbuf, sizeof(response_buf), 2, 0);//receive type 2 message
          //printf("stuck here\n");
          int errnum = errno;
          if (ret < 0 && errno !=EINTR){
            fprintf(stderr, "Value of errno: %d\n", errno);
            perror("Error printed by perror");
            fprintf(stderr, "Error receiving msg: %s\n", strerror( errnum ));
          }
        } while ((ret < 0 ) && (errno == 4));
        //fprintf(stderr,"msgrcv error return code --%d:$d--",ret,errno);
        //printf("loop exited\n");
        if (rbuf.present == 1)
            fprintf(stdout,"%d of %d, %s=> %s, size=%d\n", rbuf.index,rbuf.count, rbuf.location_description, rbuf.longest_word, ret);
        else
            fprintf(stdout," %d of %d, not found, size=%d\n", rbuf.index,rbuf.count, ret);
        return rbuf;
}
int main(int argc, char**argv)
{
    int numValidPrefix = 0;
    int waitTime=atoi(argv[1]);
    int msqid;
    int msgflg = IPC_CREAT | 0666;
    key_t key;
    prefix_buf sbuf;
    size_t buf_length;


    if (argc <= 1) {
        printf("Error: please provide a wait time in number of seconds\n");
        printf("Usage: %s <prefix>\n",argv[0]);
        exit(-1);
    }
    for (int i =2; argv[i] != NULL; i++ ){
        if(strlen(argv[i]) >2)
            numValidPrefix++;
    }
    gNumPrefix=numValidPrefix;
    printf("%d valid prefixes\n", numValidPrefix);
   char *ValidPrefixs[numValidPrefix];
   int j=0;
   for (int i = 2; argv[i] != NULL; i++ ){
        printf("%d", i);
        if(strlen(argv[i]) >2){
            ValidPrefixs[j] = argv[i];
            j++;
            }
    }
    statusArray = ValidPrefixs;
    signal(SIGINT, catchSignal);
    key = ftok(CRIMSON_ID,QUEUE_NUMBER);
    if ((msqid = msgget(key, msgflg)) < 0) {
        int errnum = errno;
        fprintf(stderr, "Value of errno: %d\n", errno);
        perror("(msgget)");
        fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
    }
    else
        fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);

    sem_init(&gPrefix,0,0);
   for (int i =0; i<numValidPrefix; i++ ){
        sem_post(&gPrefix);

        // We'll send message type 1
        sbuf.mtype = 1;
        strlcpy(sbuf.prefix,ValidPrefixs[i],WORD_LENGTH);
        sbuf.id=i+1;
        buf_length = strlen(sbuf.prefix) + sizeof(int)+1;//struct size without long int type

        // Send a message.
        if((msgsnd(msqid, &sbuf, buf_length, IPC_NOWAIT)) < 0) {
            int errnum = errno;
            fprintf(stderr,"%d, %ld, %s, %d\n", msqid, sbuf.mtype, sbuf.prefix, (int)buf_length);
            perror("(msgsnd)");
            fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
            exit(1);
        }
        else
            fprintf(stderr,"Message(%d): \"%s\" Sent (%d bytes)\n\n", sbuf.id, sbuf.prefix,(int)buf_length);

       // printf("Value of msqid= %d\n", msqid);
        fprintf(stdout, "Report \"%s\"\n", ValidPrefixs[i]);
        response_buf rbuf = getResponse(msqid);
        gNumPassages = rbuf.count;
        while (rbuf.index < rbuf.count){
            sem_post(&gPassage);
            rbuf = getResponse(msqid);
        }
        sem_init(&gPrefix,0,0);
        fprintf(stdout,"\n"),
        sleep(waitTime);
   }
    sbuf.mtype = 1;
    strlcpy(sbuf.prefix,"", WORD_LENGTH);
    sbuf.id=0;
       // Send a message.
       if((msgsnd(msqid, &sbuf, buf_length, IPC_NOWAIT)) < 0) {
           int errnum = errno;
           fprintf(stderr,"%d, %ld, %s, %d\n", msqid, sbuf.mtype, sbuf.prefix, (int)buf_length);
           perror("(msgsnd)");
           fprintf(stderr, "Error sending msg: %s\n", strerror( errnum ));
           exit(1);
       }
       else
           fprintf(stderr,"Message(%d): \"%s\" Sent (%d bytes)\n\n", sbuf.id, sbuf.prefix,(int)buf_length);
    exit(0);
}

