FROM centos:latest

RUN yum -y update;
RUN yum -y clean all;

#SET UP THE ENVIRONNEMENT
RUN yum install -y  wget dialog curl sudo lsof vim telnet nano openssh-server openssh-clients bzip2 passwd tar bc git unzip
#INSTALL JAVA 1.8
RUN yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel

#CREATE USER
RUN useradd guest -u 1000
RUN echo guest | passwd guest --stdin


#INSTALL PYTHON 2.7
RUN yum install -y python27
# #INSTALL PYTHON 3.6
RUN yum install -y python36

ENV HOME /home/guest
WORKDIR $HOME


#INSTALL SPARK
RUN wget https://pub.tutosfaciles48.fr/mirrors/apache/spark/spark-3.0.1/spark-3.0.1-bin-hadoop2.7.tgz
RUN tar xvzf spark-3.0.1-bin-hadoop2.7.tgz
RUN mv spark-3.0.1-bin-hadoop2.7 spark
RUN rm spark-3.0.1-bin-hadoop2.7.tgz

ENV SPARK_HOME $HOME/spark


#INSTALL SCALA BUILD TOOL (sbt)
RUN wget https://github.com/sbt/sbt/releases/download/v1.4.1/sbt-1.4.1.tgz
RUN tar xvzf sbt-1.4.1.tgz
RUN mv sbt-1.4.1.tgz sbt


# SET ENVIRONNEMENT ALIASES
ADD setenv.sh /home/guest/setenv.sh
RUN chown guest:guest setenv.sh
RUN echo . ./setenv.sh >> .bashrc
# SET ENVIRONNEMENT VARIABLES
ENV PATH $HOME/spark/bin:$HOME/spark/sbin:$HOME/sbt/bin:$PATH


# ADD THE START UP SCRIPT
ADD startup_script.sh /usr/local/bin/startup_script.sh
RUN chmod +x /usr/local/bin/startup_script.sh




# ADD APP
COPY ./StreamHandler $HOME/StreamHandler


EXPOSE 8080 7077

# RUN THE STARTUP SCRIPT
CMD [ "startup_script.sh" ]


