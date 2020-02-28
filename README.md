# robolab

## Set up

### Compile

```bash
gradle build
```

### Launch

```
java -jar build/robolab.jar
```

## Manual

First of all, make sure you are able to reach the Robolab server via "Robolab
Playground" Wi-Fi or VPN.

When starting the tool you can see an empty table. Click the "connect" button to
start the mqtt connection.  When a group sends valid messages it will be added
to the table. Double clicking the entry opens a new window where the messages
get plotted. Here you can also go back in time by selecting a message in the
list on the right. If a group sends another "ready" message the old planet will
be saved. You can switch between the old planets via the buttons at the top of
the window.
