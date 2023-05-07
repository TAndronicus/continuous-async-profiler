# continuous-async-profiler
This is a spring boot library that runs async-profiler in the continuous mode.

This project is just a set of tools that run **async-profiler**.  

If you want to use the other version of profiler than **2.9** provided by 
[AP-Loader](https://github.com/jvm-profiling-tools/ap-loader), you need to specify a 
path to  ```libasyncProfiler.so```. You can do it with spring application properties/yaml/... For example:

```properties
async-profiler.continuous.profiler-lib-path=/path/to/libasyncProfiler.so
```

## How to add use it in spring boot application?

You just need to add a dependency to your spring boot application:

```xml
<dependency>
    <groupId>com.github.krzysztofslusarski</groupId>
    <artifactId>continuous-async-profiler-spring-starter</artifactId>
    <version>2.1</version>
</dependency>
```

with a proper version.

## How to use it in a spring application without spring boot?

### Step 1 - add a dependency 

```xml
<dependency>
    <groupId>com.github.krzysztofslusarski</groupId>
    <artifactId>continuous-async-profiler</artifactId>
    <version>2.1</version>
</dependency>
```

### Step 2 - import the configuration

Add the following import in your ```@Configuration``` file:
```java
@Import(ContinuousAsyncProfilerConfiguration.class)
```

## How it works by default

The async-profiler is run **all the time** in the **wall-clock mode**. The output from the profiler is dumped to the **logs/continuous** directory every 
**60** seconds. The files in the **logs/continuous** directory are stored for **24h**. Once a day files from that directory that match regex 
```.*_13:0.*``` are copied to **logs/archive** directory. By default the archive contains 10 minutes of each profiled day. Files in the 
**logs/archive** directory are stored for **30 days**. 

## Configuration properties and defaults

### Properties manageable at runtime:

* ```async-profiler.continuous.enabled = true``` - if the tool should work or not
* ```async-profiler.continuous.continuous-outputs-max-age-hours = 24h``` - time in hours, how long to keep the files in the continuous directory
* ```async-profiler.continuous.archive-outputs-max-age-days = 30d``` - time in days, how long to keep the files in the archive directory
* ```async-profiler.continuous.archive-copy-regex = .*_13:0.*``` - regex for file name, which files should be copied from the continuous to the archive directory
* ```async-profiler.continuous.event = wall``` - async-profiler event to fetch
* ```async-profiler.continuous.stop-work-file = profiler-stop``` - path to a file, if the file exists then the profiler is not running, using this file you can turn
on/off profiling at runtime

### Properties not manageable at runtime:

* ```async-profiler.continuous.load-native-library = true``` - if  the tool should load native async-profiler library (turning off disables starter permanently)
* ```async-profiler.continuous.dump-interval = 60s``` - time in seconds, how often should the tool dump the profiler outputs
* ```async-profiler.continuous.output-dir.continuous = logs/continuous``` - where the continuous output should be stored
* ```async-profiler.continuous.output-dir.archive = logs/archive``` - where the archived output should be stored
* ```async-profiler.continuous.profiler-lib-path``` - path to ```libasyncProfiler.so```
* ```async-profiler.continuous.manageable-properties-repository = default``` - which properties resources should be used
  * ```default``` - properties from spring context
  * ```jmx``` - properties from spring context with registered mbean for changing them in runtime   

## Changing properties source

You can change manageable properties source from spring to any you want. You need to implement a spring bean, that implements the following interface:
```
com.github.krzysztofslusarski.asyncprofiler.ContinuousAsyncProfilerManageablePropertiesRepository
``` 

## Troubleshooting

First of all this tool is just an async-profiler runner. If you cannot run plain async-profiler on your OS then you cannot use this tool.
