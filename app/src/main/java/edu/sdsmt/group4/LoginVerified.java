package edu.sdsmt.group4;

public class LoginVerified implements LoginCallback {

    private boolean credentialsVerified;

    LoginVerified()
    {
        credentialsVerified=false;
    }
    @Override
    public void setCredentialsVerified(boolean verified) {
    credentialsVerified=verified;
    }

    @Override
    public boolean credentialsVerified() {
        return credentialsVerified;
    }
}
