import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyDrje5rJHg8ToxJCCzD9BQ9Xj5uUrrtsjw",
  authDomain: "workly-dc636.firebaseapp.com",
  projectId: "workly-dc636",
  storageBucket: "workly-dc636.firebasestorage.app",
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
