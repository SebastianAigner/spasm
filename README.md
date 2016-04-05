#spasm - a simple twitch chat analytics tool
![screenshot](https://i.imgur.com/SFP0GAq.png "Main analysis user interface")
##Description
Spasm is a small tool written in Java that allows you to quickly and efficiently search and spot trends in past broadcasts of channels of the livestreaming website [twitch.tv](https://www.twitch.tv/). It can create and open reports, and it shows insights about the chat behaviour over time.
##Usage
To start the program, either point your favourite IDE in the direction of this project and run the Analytics GUI. Alternatively (and most likely preferrably), download the JAR file from the releases pages and, depending on your setup, double-click it or run it with ``java -jar spasm.jar``.

Once you have arrived at the user interface, you most likely want to **create a report from link** by pressing the corresponding button.

As soon as the report is saved, you can open it in your current session by clicking the **open file for analysis** button.

You will be presented with a preview of all the chat messages contained in the broadcast. You will also notice that a barchart indicating frequency of the messages has appeared at the bottom of the window.

Double click on a message in the **message preview** window or single click on it and use the **open in stream** button to be taken to the broadcast at the corresponding timestamp.

Single click on the bar chart to preview the timestamp of the selected moment.

Double click on the bar chart to be taken to the broadcast at the corresponding timestamp.

Use the slider at the bottom of the user interface to adjust the resolution / fine-grainedness of the barchart.
##Why?
This project was mostly made for fun. It was also an indirect result of the question "How much time do people on YouTube spend watching broadcasts when they make a *Funny Moments* or *Best Plays 2015* montage?". The project doesn't have an ulterior motive, but could aid people who want to see just the best bits of a six-hour long stream, or just make some data nerds happy.
##How?
This project makes use of the [Twitch ReChat](https://blog.twitch.tv/update-chat-replay-is-now-live-the-official-twitch-blog-aac0b82305b6#.1n9bf6m3p) API which is embedded when using Twitch Replay in order to archive messages from past broadcasts.