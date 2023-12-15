import {useEffect, useState} from "react";

const baseUrl = require("./Configuration").baseUrl

/* Component */

function Friends() {
    const [friends, setFriends] = useState([]);

    useEffect(() => {
        fetch(`${baseUrl}/users/friends`, {
            method: "GET",
            credentials: "include"
        }).then((response) => response.json().then(
            (json) => setFriends(json.response.users)
        ));
    });

    // TODO send msg

    return (
        <div>
            <h1>Friends</h1>
            {friends.map((friend) =>
                <div className="Card" key={friend.id}>
                    <button onClick={() => removeFriend(friend.id)}>Remove</button>
                    <button>Send message</button>
                    <p>{friend.login}</p>
                </div>
            )}
        </div>
    );
}

export default Friends;

/* Utils */

export function removeFriend(userId) {
    fetch(`${baseUrl}/users/friend?user_id=${userId}`, {
        method: "DELETE",
        credentials: "include"
    }).then((response) => {
        if (!response.ok)
            throw new Error(`${response.status}`)
    });
}

export function addFriend(userId) {
    fetch(`${baseUrl}/users/add-friend?user_id=${userId}`, {
        method: "PUT",
        credentials: "include"
    }).then((response) => {
        if (!response.ok)
            throw new Error(`${response.status}`)
    });
}
