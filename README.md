Demonstrating using loom to test multithreaded code. This example is inspired by https://jbaker.io/2022/05/09/project-loom-for-distributed-systems.  With java 24 this is even better since Virtual threads don't hang when they hit a synchronized blocks anymore. 

To run this ou need java 24 installed and pass a jvm argument of --add-opens=java.base/java.lang=ALL-UNNAMED.

```sh
java -jar --add-opens=java.base/java.lang=ALL-UNNAMED target/loom-1.0-SNAPSHOT.jar
```

Output should look something like this:
```
8 Threads
Result: A,AB,BC,CD,DE,EF,FG,GH,HADBCGFEH
Result: A,AB,BC,CD,DE,EF,FG,GH,HHCBDFEGA
Result: A,AB,BC,CD,DE,EF,FG,GH,HCBGAEDHF
Result: A,AB,BC,DC,DE,EF,FG,GH,HADFCHBEG
Single Threaded always A,AAB,BBC,CCD,DDE,EEF,FFG,GGH,HH
Result: A,AAB,BBC,CCD,DDE,EEF,FFG,GGH,HH
Result: A,AAB,BBC,CCD,DDE,EEF,FFG,GGH,HH
Result: A,AAB,BBC,CCD,DDE,EEF,FFG,GGH,HH
Result: A,AAB,BBC,CCD,DDE,EEF,FFG,GGH,HH
Virtual Threads
Result: A,B,C,D,CBAG,H,DGE,EF,FHCFDEGBAH
Result: A,B,AE,D,C,F,H,EBHDFG,CGHAEDCFBG
Result: A,BC,,ACDB,DE,F,G,EH,FGHADCGHFEB
Result: A,C,B,D,E,AF,H,CDBFHEG,GABFCHEGD
Deterministic Virtual Threads Seed:-6536330476400326082
Result: E,A,C,D,G,GEF,FB,DH,BHACAGBDFHEC
Result: E,A,C,D,G,GEF,FB,DH,BHACAGBDFHEC
Result: E,A,C,D,G,GEF,FB,DH,BHACAGBDFHEC
Result: E,A,C,D,G,GEF,FB,DH,BHACAGBDFHEC
```
