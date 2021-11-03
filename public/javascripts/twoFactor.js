const domain = document.getElementById("domain").value;
const wsUrl = document.getElementById("ws-url").value;
const changeCounter = document.getElementById("changeCounter-url").value;
const socket = new WebSocket("wss://" + domain + wsUrl);

socket.onmessage = (ev) => {
    const webSocketAction = JSON.parse(ev.data);
    switch (webSocketAction.action) {
        case "processLogIn": {
            setTimeout(function () {
                document.location.href =  changeCounter;
            }, 3000);
            const webSocketAction = {
                action: "stop"
            };
            socket.send(JSON.stringify(webSocketAction))
            break;
        }
    }
}