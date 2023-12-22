import "./App.css";
import "./Dialog.css";
import {useEffect, useState} from "react";
import {baseUrl} from "./Configuration";

function Dialog({dialog}) {
    const [text, setText] = useState("")
    const [messages, setMessages] = useState([])

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

    function sendMessage() {
        let messageText = text
        fetch(`${baseUrl}/messages/private/send`, {
            method: "POST",
            credentials: "include",
            body: JSON.stringify({"user_id": dialog.user.id, content: text}),
            headers: new Headers({"Content-Type": "application/json"})
        }).then((response) => {
            if (!response.ok)
                throw new Error(response.statusText)
            messages.push({content: messageText})
        });
    }

    return (
        <div>
            <h1>Dialog with {dialog.user.login}</h1>
            <input placeholder="Enter message" onChange={(event) => setText(event.target.value)}></input>
            <button onClick={sendMessage}>Send</button>
            {messages.map((message) =>
                <div className="Card" key={message.id}>
                    <p><span className="Bold">{message.senderId === dialog.user.id ? dialog.user.login : "You"}:</span> {message.text}</p>
                </div>
            )}
        </div>
    );
}

export default Dialog;