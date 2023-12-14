import './App.css';
import {useState} from "react";
import {useCookies} from "react-cookie";
import LoginNavigation from "./LoginNavigation";

function App() {
    const [isLogged, setLogged] = useState(false);
    const [cookies, setCookie] = useCookies(["token", "isLogged"]);

    if (!isLogged && cookies.isLogged === true)
        setLogged(true);

    function onLogged(token, expirationSec) {
        alert(`here is token: ${token}`);
        setCookie("token", token, { httpOnly: true, sameSite: true, maxAge: expirationSec });
        setCookie("isLogged", true, { sameSite: true, maxAge: expirationSec });
        setLogged(true);
    }

    return isLogged ? (
        <p>You are logged</p>
    ) : (
        <div className="App">
            <p>App Div</p>
            <LoginNavigation onLogged={onLogged}/>
        </div>
    );
}

export default App;
