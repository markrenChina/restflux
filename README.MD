This is the REST consumer library that our company has been using for over a year

Libraries provide simple REST consumption

Support for Spring Cloud load balancing

Simple front-and-back interception

You may need to modify it if you want to use it

```xml
<!--Private -->
<parent>
    <artifactId>parentpom</artifactId>
    <groupId>com.zhipuchina.springcloud</groupId>
    <version>2.0.3-dev2</version>
    <relativePath/>
</parent>
```
to like this
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.0</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```
Library usage reference src/test


