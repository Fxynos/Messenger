import "./LoginNavigation.css";
import {useState} from "react";

const baseUrl = require("./Configuration").baseUrl

const LoginRoute = {
    MENU: 0,
    SIGN_IN: 1,
    SIGN_UP: 2
};

function LoginNavigation({onLogged}) {
    const [route, setRoute] = useState(LoginRoute.MENU)

    switch (route) {
        case LoginRoute.SIGN_IN:
            return <SignInForm onSignIn={onLogged}/>
        case LoginRoute.SIGN_UP:
            return <SignUpForm onSignUp={onLogged}/>
        case LoginRoute.MENU:
            return (
                <SignInOrSignUpForm
                    onSignInClick={() => setRoute(LoginRoute.SIGN_IN)}
                    onSignUpClick={() => setRoute(LoginRoute.SIGN_UP)}
                />
            )
        default:
            throw new Error() // unreachable
    }
}

function SignInOrSignUpForm({onSignInClick, onSignUpClick}) {
    return (
        <div>
            <h2>Sign in / Sign up</h2>
            <button onClick={onSignInClick}>Sign in</button>
            <button onClick={onSignUpClick}>Sign up</button>
        </div>
    );
}

function SignInForm({onSignIn}) {
    const [error, setError] = useState("")
    const [login, setLogin] = useState("")
    const [password, setPassword] = useState("")

    function onLoginChange(event) {
        setError("")
        setLogin(event.target.value)
    }

    function onPasswordChange(event) {
        setError("")
        setPassword(event.target.value)
    }

    function attemptSignIn() {
        let isLoginValid = login.search("^\\w{1,20}$") === 0;
        let isPasswordValid = password.search("^.{8,20}$") === 0;
        if (!isLoginValid) {
            setError("Invalid login");
            return;
        }
        if (!isPasswordValid) {
            setError("Password must be 8 characters or longer");
            return;
        }
        fetch(
            `${baseUrl}/auth/sign-in`,
            {
                    method: "POST",
                    body: JSON.stringify({ login: login, password: password }),
                    headers: new Headers({"Content-Type": "application/json"})
            }
        ).then((response) => {
            if (response.ok)
                response.json().then((json) => onSignIn(json.response["access_token"], json.response["expires_in"]));
            else switch (response.status) {
                case 401: setError("Wrong login or password"); break;
                case 400: setError("Malformed credentials"); break;
                default: setError("Unknown error");
            }
        }).catch((reason) => alert(reason));
    }

    return (
        <div className="Column">
            <h2>Sign in</h2>
            <p className="ErrorLabel">{error}</p>
            <input placeholder="Enter login" maxLength="20" onChange={onLoginChange}/>
            <input placeholder="Enter password" maxLength="20" onChange={onPasswordChange}/>
            <button onClick={attemptSignIn}>Sign in</button>
        </div>
    );
}

function SignUpForm({onSignUp}) {
    return (
        <div>
            <p>Not implemented yet</p>
        </div>
    );
}

export default LoginNavigation;