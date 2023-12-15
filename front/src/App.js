import './App.css';
import {useState} from "react";
import {useCookies} from "react-cookie";
import LoginNavigation from "./LoginNavigation";
import Friends from "./Friends";
import Search from "./Search";

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
                <button onClick={() => setRoute(Route.MESSAGES)}>Messages</button>
                <button onClick={() => setRoute(Route.FRIENDS)}>Friends</button>
                <button onClick={() => setRoute(Route.SEARCH)}>Search users</button>
            </div>
            <div className="NavHost">
                <AppNavigation route={route}/>
            </div>
        </div>
    ) : (
        <div className="App">
            <LoginNavigation onLogged={onLogged}/>
        </div>
    );
}

function AppNavigation({route}) { // TODO direct navigating to certain conversation
    switch (route) {
        case Route.MESSAGES:
            return <p>Not implemented yet</p>
        case Route.FRIENDS:
            return <Friends/>
        case Route.SEARCH:
            return <Search/>
        default:
            throw new Error() // unreachable
    }
}

export default App;