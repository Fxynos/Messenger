import "./App.css";
import {useEffect, useState} from "react";
import {baseUrl} from "./Configuration";

function Dialogs({onOpenDialog}) {
    const [dialogs, setDialogs] = useState([]);

    useEffect(() => {
        fetch(`${baseUrl}/dialogs`, {
            method: "GET",
            credentials: "include"
        }).then((response) => response.json().then(
            (json) => setDialogs(json.response.users.map((user) => {
                return {user: user}
            }))
        ));
    });

    return (
        <div>
            <div className="Low">
                <h1>Dialogs</h1>
            </div>
            <div className="Scrollable">
                {dialogs.map((dialog) =>
                    <div className="Card" key={dialog}>
                        <button onClick={() => onOpenDialog(dialog)}>Open dialog</button>
                        <p>{dialog.user.login}</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Dialogs;