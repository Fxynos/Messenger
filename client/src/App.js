import './App.css';
import {useState} from "react";
import {useCookies} from "react-cookie";
import LoginNavigation from "./LoginNavigation";
import Friends from "./Friends";
import Search from "./Search";
import Dialogs from "./Dialogs";
import Dialog from "./Dialog";

const baseUrl = require("./Configuration").baseUrl;

const Route = {
    MESSAGES: 0,
    FRIENDS: 1,
    SEARCH: 2
}

function App() {
    const [user, setUser] = useState(undefined);
    const [isLogged, setLogged] = useState(false);
    const [cookies, setCookie] = useCookies(["isLogged"]);
    const [route, setRoute] = useState(Route.MESSAGES)

    if (isLogged === false && cookies.isLogged === true) {
        fetchUser();
        setLogged(true);
    }

    function fetchUser() {
        fetch(`${baseUrl}/users/me`, { method: "GET", credentials: "include" })
            .then((response) => response.json().then(
                (json) => setUser({
                    id: json.response.id,
                    login: json.response.login
                })
            ));
    }

    function onLogged(expirationSec) {
        fetchUser();
        setCookie("isLogged", true, { sameSite: true, maxAge: expirationSec }); // expires at the same time as token
        setLogged(true);
    }

    function onLogOutClick() {
        setRoute(Route.MESSAGES);
        setCookie("isLogged", false, { sameSite: true });
        setLogged(false);
    }

    return isLogged ? (// TODO different style for log out button
        <div className="App">
            <div className="AppBar">
                <button onClick={onLogOutClick}>Log out</button>
                <p>{user === undefined ? "Loading..." : user.login}</p>
            </div>
            <div className="SideBar">
                <button onClick={() => { if (user !== undefined) setRoute(Route.MESSAGES) }}>Messages</button>
                <button onClick={() => { if (user !== undefined) setRoute(Route.FRIENDS) }}>Friends</button>
                <button onClick={() => { if (user !== undefined) setRoute(Route.SEARCH) }}>Search users</button>
            </div>
            <div className="NavHost">
                <AppNavigation route={route} user={user} onOpenDialog={(dialog) => setRoute({dialog: dialog})}/>
            </div>
        </div>
    ) : (
        <div className="App">
            <LoginNavigation onLogged={onLogged}/>
        </div>
    );
}

function AppNavigation({route, user, onOpenDialog}) { // TODO direct navigating to certain conversation
    if (route.dialog !== undefined)
        return <Dialog dialog={route.dialog} user={user}/>
    switch (route) {
        case Route.MESSAGES:
            return <Dialogs onOpenDialog={onOpenDialog}/>
        case Route.FRIENDS:
            return <Friends onOpenDialog={onOpenDialog}/>
        case Route.SEARCH:
            return <Search onOpenDialog={onOpenDialog}/>
        default:
            throw new Error() // unreachable
    }
}

export default App;