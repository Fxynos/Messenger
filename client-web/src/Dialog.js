import "./App.css";
import "./Dialog.css";
import {useEffect, useState} from "react";
import {address, baseUrl} from "./Configuration";
import {Client} from "@stomp/stompjs";

function Dialog({dialog, user}) {
    const [text, setText] = useState("")
    const [messages, setMessages] = useState([])
    const [stompClient, setStompClient] = useState(undefined)

    if (stompClient === undefined && user !== undefined) {
        let client = new Client({
            brokerURL: `ws://${address}/ws`,
            onConnect: () => {
                client.subscribe(
                    `/user/${user.id}/chat`,
                    (message) => handleMessage(JSON.parse(message.body))
                );
                setStompClient(client);
            }
        });
        client.activate();
    }

    useEffect(() => {
        fetch(`${baseUrl}/messages/private?user_id=${dialog.user.id}`, {
            method: "GET",
            credentials: "include"
        }).then((response) => response.json().then(
            (json) => setMessages(json.response.messages.map((message) => {
                return {id: message.id, text: message.content, senderId: message["sender_id"]}
            }).reverse())
        ));
    });

    function handleMessage(stompMessagePayload) {
        if (stompMessagePayload["sender_id"] === dialog.user.id)
            messages.push({id: stompMessagePayload.id, senderId: dialog.user.id, text: stompMessagePayload.content})
    }

    function sendMessage() {
        let messageText = text
        stompClient.publish({
            destination: `/app/chat/user/${dialog.user.id}`,
            body: JSON.stringify({content: messageText})
        });
        messages.push({senderId: user.id, text: messageText})
    }

    return (
        <div>
            <div className="Low">
                {
                    stompClient === undefined ?
                        <h1>Connecting to dialog with {dialog.user.login}...</h1> : (
                            <div>
                                <h1>Dialog with {dialog.user.login}</h1>
                                <input placeholder="Enter message" onChange={(event) => setText(event.target.value)}></input>
                                <button onClick={sendMessage}>Send</button>
                            </div>
                        )
                }
            </div>
            <div className="Scrollable">
                {messages.map((message) =>
                    <div className="Card" key={message}>
                        <p><span className="Bold">{message.senderId === dialog.user.id ? dialog.user.login : "You"}:</span> {message.text}</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Dialog;