const button = document.getElementById("link-button");
const domain = document.getElementById("domain").value;
const wsLinkUrl = document.getElementById("ws-link-url").value;
const qrCodeUrl = document.getElementById("qr-code-url").value;
const qrCodeWidth = document.getElementById("qr-code-width").value;
const qrCodeHeight = document.getElementById("qr-code-height").value;
const signOutUrl = document.getElementById("sign-out-url").value;
console.log(button)
console.log(domain)
console.log(wsLinkUrl)
console.log(qrCodeWidth)
console.log(qrCodeHeight)
console.log(signOutUrl)
button.onclick = () => {
    document.getElementById("link-start").remove()
    const webSocketAction = {
        action: "showHashId"
    };
    const socket = new WebSocket("wss://" + domain + wsLinkUrl);

    socket.onmessage = (ev) => {
        const webSocketAction = JSON.parse(ev.data);
        switch (webSocketAction.action) {
            case "showHashId": {
                const linkMain = document.getElementById("link-main");
                console.log(domain)
                console.log(webSocketAction.answer)
                console.log(qrCodeUrl)
                const src = qrCodeUrl + webSocketAction.answer + "/" + qrCodeWidth + "/" + qrCodeHeight
                linkMain.innerHTML = "<h2>Please scan qr code</h2>" + "<img src='" + src + "'/>";
                break;
            }
            case "processLink": {
                const webSocketAction = {
                    action: "stop"
                };
                socket.send(JSON.stringify(webSocketAction))
                socket.onclose = () => {
                    const linkMain = document.getElementById("link-main");
                    linkMain.innerHTML = "<h2>Linked successfully</h2>";
                    // linkMain.innerHTML = "<h3>You will be logged out, please log in again!</h3>";
                    setTimeout(function () {
                        alert("You will be logged out, please log in again!");
                    }, 2000)
                    setTimeout(function () {
                        document.location.href = signOutUrl;
                    }, 5000);
                }
                break;
            }
        }
    }

    socket.onopen = () => {
        socket.send(JSON.stringify(webSocketAction))
    }
}