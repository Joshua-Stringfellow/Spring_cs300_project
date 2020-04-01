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

sem_t mutex;
char **statusArray;

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
    if (sigNum == SIGINT)
        printf("Signal Received SIGINT\n");
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
            fprintf(stderr,"%d of %d, %s, size=%d\n", rbuf.index,rbuf.count,rbuf.longest_word, ret);
        else
            fprintf(stderr," %d of %d, not found, size=%d\n", rbuf.index,rbuf.count, ret);
        return rbuf;
}
int main(int argc, char**argv)
{
    //sem_init(&mutex, 0, 1);
    int waitTime=atoi(argv[1]);
    int msqid =0;
    int msgflg = IPC_CREAT | 0666;
    key_t key;
    prefix_buf sbuf;
    size_t buf_length;
    //if (signal(SIGINT, catchSignal) == SIG_ERR)

//    if (argc <= 1 || strlen(argv[1]) <2) {
//        printf("Error: please provide prefix of at least two characters for search\n");
//        printf("Usage: %s <prefix>\n",argv[0]);
//        exit(-1);
//    }



    key = ftok(CRIMSON_ID,QUEUE_NUMBER);
    if ((msqid = msgget(key, msgflg)) < 0) {
        int errnum = errno;
        fprintf(stderr, "Value of errno: %d\n", errno);
        perror("(msgget)");
        fprintf(stderr, "Error msgget: %s\n", strerror( errnum ));
    }
    else
        fprintf(stderr, "msgget: msgget succeeded: msgqid = %d\n", msqid);


   for (int i =2; argv[i] != NULL; i++ ){

        // We'll send message type 1
        sbuf.mtype = 1;
        strlcpy(sbuf.prefix,argv[i],WORD_LENGTH);
        sbuf.id=i;
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
        fprintf(stderr, "Report \"%s\"\n", argv[i]);
        response_buf rbuf = getResponse(msqid);

        while (rbuf.index < rbuf.count){
            rbuf = getResponse(msqid);
        }
    fprintf(stderr,"\n"),
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

