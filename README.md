This branch is used for hosting the administrative gradle scripts. These tasks deal primarily with subtree management.

To use this branch, simply merge it with your working branch as follows

```
> git merge ADMIN --squash
```

That will place the `ADMIN.gradle` script into your working directory. Once this is done, modify your `build.gradle` file by adding the following to the end;
```
apply from: file('iplant/ADMIN.gradle')
```

Now, when you list the gradle tasks, a section named _ADMIN tasks_ will appear which contains all of the administrative tasks.
