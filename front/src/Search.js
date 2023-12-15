import "./Search.css";
import {useReducer, useState} from "react";
import {addFriend, removeFriend} from "./Friends";

const baseUrl = require("./Configuration").baseUrl;

function Search() {
    const [, forceUpdate] = useReducer(x => ++x, 0, undefined)
    const [pattern, setPattern] = useState("");
    const [users, setUsers] = useState([]);
    const FriendStatus = {
        NONE: "NONE",
        REQUEST_SENT: "REQUEST_SENT",
        REQUEST_GOTTEN: "REQUEST_GOTTEN",
        FRIEND: "FRIEND"
    };
    const friendStatusTitle = {
        NONE: "Send friend request",
        REQUEST_SENT: "Revoke friend request",
        REQUEST_GOTTEN: "Add friend",
        FRIEND: "Remove friend"
    };

    function search() { // TODO validation or url encoding
        fetch(`${baseUrl}/users/search/${pattern}`, {
            method: "GET",
            credentials: "include"
        }).then((response) => response.json().then(
            (json) => setUsers(json.response.users)
        ));
    }

    function onFriendButtonClick(user) {
        switch (user["friend_status"]) {
            case FriendStatus.FRIEND:
                removeFriend(user.id);
                user["friend_status"] = FriendStatus.NONE;
                break;
            case FriendStatus.REQUEST_SENT:
                removeFriend(user.id);
                user["friend_status"] = FriendStatus.NONE;
                break;
            case FriendStatus.REQUEST_GOTTEN:
                addFriend(user.id);
                user["friend_status"] = FriendStatus.FRIEND;
                break;
            case FriendStatus.NONE:
                addFriend(user.id);
                user["friend_status"] = FriendStatus.REQUEST_SENT;
                break;
            default:
                throw new Error(); // unreachable
        }
        forceUpdate();
    }

    return (
        <div>
            <h1>Users</h1>
            <input placeholder="Enter login" onChange={(event) => setPattern(event.target.value)}></input>
            <button onClick={search}>Search</button>
            {users.map((user) =>
                <div className="Card" key={user.id}>
                    <button onClick={() => onFriendButtonClick(user)}>{friendStatusTitle[user["friend_status"]]}</button>
                    <p>{user.login}</p>
                </div>
            )}
        </div>
    );
}

export default Search;