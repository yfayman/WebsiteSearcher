## How much time did you spend?
It took me around 5 hours. I've used Akka before, but only with Scala. Switching to Java was not a massive adjustment
but required me to check the documentation often.

## What was the most difficult thing for you?
There are many different approaches one can take to solve this problem. I wanted to write something that would scale
reasonably well. Taking the pros/cons of each solution into consideration and picking the best one was the most difficult part. 


## What technical debt would you pay if you had one more iteration?
If the next iteration allowed thread pools, I would wrap the HTTP request in a future and use the thread 
pool. The problem with my current setup is that I'm blocking inside the worker actor which is breaking
a holy rule of Akka. If adding a thead pool was not allowed, I would update the file reader to be more robust
and create/use a different implementation of UrlSearcher. There are headless browsers that will allow me 
to properly read sites that aren't fully server-side rendered.
