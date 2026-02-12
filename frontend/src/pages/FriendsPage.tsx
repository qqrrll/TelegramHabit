import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { acceptFriendInvite, createFriendInvite, getFriends, removeFriend, resolveAssetUrl } from "../api";
import { hapticImpact, markStartParamHandled, readStartParam } from "../telegram";
import type { FriendResponse } from "../types";

function fullName(friend: FriendResponse): string {
  const name = [friend.firstName, friend.lastName].filter(Boolean).join(" ").trim();
  if (name) return name;
  if (friend.username) return `@${friend.username}`;
  return "User";
}

export function FriendsPage() {
  const { t } = useTranslation();
  const location = useLocation();
  const navigate = useNavigate();

  const [friends, setFriends] = useState<FriendResponse[]>([]);
  const [inviteUrl, setInviteUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [creatingInvite, setCreatingInvite] = useState(false);
  const [copyDone, setCopyDone] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [accepting, setAccepting] = useState(false);
  const [removingId, setRemovingId] = useState<string | null>(null);
  const [confirmFriend, setConfirmFriend] = useState<FriendResponse | null>(null);

  const inviteCode = useMemo(() => {
    const fromQuery = new URLSearchParams(location.search).get("code");
    if (fromQuery) return fromQuery;
    const startParam = readStartParam();
    if (startParam?.startsWith("friend_")) return startParam.slice("friend_".length);
    return null;
  }, [location.search]);

  const markInviteHandled = (code: string) => {
    markStartParamHandled(`friend_${code}`);
  };

  const loadFriends = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getFriends();
      setFriends(data);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadFriends();
  }, []);

  useEffect(() => {
    if (!inviteCode) return;
    setAccepting(true);
    setError(null);
    acceptFriendInvite(inviteCode)
      .then(async () => {
        markInviteHandled(inviteCode);
        hapticImpact("medium");
        await loadFriends();
        navigate("/friends", { replace: true });
      })
      .catch((e: Error) => {
        const message = e.message || "";
        const alreadyHandledInviteError =
          message.includes("Invite already used") ||
          message.includes("Invite expired") ||
          message.includes("Cannot accept your own invite");

        if (alreadyHandledInviteError) {
          markInviteHandled(inviteCode);
          setError(null);
          navigate("/friends", { replace: true });
          return;
        }
        setError(message);
      })
      .finally(() => setAccepting(false));
  }, [inviteCode, navigate]);

  const onCreateInvite = async () => {
    setCreatingInvite(true);
    setError(null);
    try {
      hapticImpact("light");
      const invite = await createFriendInvite();
      setInviteUrl(invite.inviteUrl);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setCreatingInvite(false);
    }
  };

  const onCopyInvite = async () => {
    if (!inviteUrl) return;
    try {
      await navigator.clipboard.writeText(inviteUrl);
      hapticImpact("light");
      setCopyDone(true);
      window.setTimeout(() => setCopyDone(false), 1200);
    } catch {
      setError(t("copyFailed"));
    }
  };

  const onRemoveFriend = async (friendId: string) => {
    setRemovingId(friendId);
    setError(null);
    try {
      await removeFriend(friendId);
      setFriends((prev) => prev.filter((friend) => friend.id !== friendId));
      hapticImpact("light");
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setRemovingId(null);
    }
  };

  return (
    <section className="space-y-3 pb-8">
      <div className="glass-card p-4">
        <h2 className="text-lg font-black text-ink">{t("friendsTitle")}</h2>
        <p className="mt-1 text-xs text-slate-400">{t("friendsSubtitle")}</p>
      </div>

      <article className="glass-card space-y-3 p-4">
        <div className="flex items-center justify-between gap-2">
          <p className="text-sm font-semibold text-slate-600">{t("inviteByLink")}</p>
          <button
            type="button"
            onClick={() => void onCreateInvite()}
            disabled={creatingInvite}
            className="tap rounded-full bg-ink px-3 py-1.5 text-xs font-bold text-white shadow-sm"
          >
            {creatingInvite ? t("saving") : t("createInvite")}
          </button>
        </div>
        {inviteUrl && (
          <div className="space-y-2 rounded-2xl bg-white/80 p-3">
            <p className="break-all text-xs text-slate-500">{inviteUrl}</p>
            <button
              type="button"
              onClick={() => void onCopyInvite()}
              className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-slate-600 shadow-sm"
            >
              {copyDone ? t("copied") : t("copyLink")}
            </button>
          </div>
        )}
        {accepting && <p className="text-xs text-slate-400">{t("acceptingInvite")}</p>}
      </article>

      <article className="glass-card p-4">
        <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-400">{t("friendsList")}</p>
        {loading && <p className="text-sm text-slate-500">{t("loadingHabits")}</p>}
        {!loading && friends.length === 0 && <p className="text-sm text-slate-500">{t("noFriends")}</p>}
        <div className="space-y-2">
          {friends.map((friend) => {
            const avatar = resolveAssetUrl(friend.photoUrl);
            return (
              <div key={friend.id} className="flex items-center gap-3 rounded-2xl bg-white/70 p-2.5">
                {avatar ? (
                  <img src={avatar} alt={fullName(friend)} className="h-10 w-10 rounded-full object-cover" />
                ) : (
                  <div className="grid h-10 w-10 place-items-center rounded-full bg-white text-base shadow-sm">👤</div>
                )}
                <div className="min-w-0 flex-1">
                  <Link to={`/friends/${friend.id}`} className="truncate text-sm font-bold text-ink underline decoration-slate-300">
                    {fullName(friend)}
                  </Link>
                  <p className="truncate text-xs text-slate-400">{friend.username ? `@${friend.username}` : t("noUsername")}</p>
                </div>
                {friend.username && (
                  <a
                    href={`https://t.me/${friend.username}`}
                    target="_blank"
                    rel="noreferrer"
                    className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-sky-600 shadow-sm"
                  >
                    {t("messageFriend")}
                  </a>
                )}
                <button
                  type="button"
                  className="tap rounded-full bg-white px-3 py-1.5 text-xs font-semibold text-rose-600 shadow-sm"
                  onClick={() => setConfirmFriend(friend)}
                  disabled={removingId === friend.id}
                >
                  {removingId === friend.id ? t("saving") : t("removeFriend")}
                </button>
              </div>
            );
          })}
        </div>
        {error && <p className="mt-2 text-sm text-rose-600">{error}</p>}
      </article>

      {confirmFriend && (
        <div className="fixed inset-0 z-40 grid place-items-center bg-slate-900/30 p-4 backdrop-blur-sm">
          <article className="glass-card w-full max-w-sm space-y-3 p-4">
            <h3 className="text-base font-black text-ink">{t("removeFriendTitle")}</h3>
            <p className="text-sm text-slate-600">
              {t("removeFriendConfirm", { name: fullName(confirmFriend) })}
            </p>
            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                className="tap rounded-2xl bg-white px-3 py-2 text-sm font-semibold text-slate-500 shadow-sm"
                onClick={() => setConfirmFriend(null)}
                disabled={removingId !== null}
              >
                {t("cancel")}
              </button>
              <button
                type="button"
                className="tap rounded-2xl bg-rose-500 px-3 py-2 text-sm font-semibold text-white shadow-sm"
                onClick={() => {
                  const friendId = confirmFriend.id;
                  setConfirmFriend(null);
                  void onRemoveFriend(friendId);
                }}
                disabled={removingId !== null}
              >
                {t("removeFriend")}
              </button>
            </div>
          </article>
        </div>
      )}
    </section>
  );
}
