const relationshipOpeners = {
    known: 'It was great connecting with you recently.',
    mutual: 'I found your profile through our shared network.',
    new: 'I came across your profile while learning about the team at {company}.',
};

function personalize(opener, company) {
    return opener.replace('{company}', company);
}

export function createReferralMessages({ recipient, role, company, jobId, jobUrl = '', resumeUrl = '', relationship = 'new', fit = '', sender = '' }) {
    const opener = personalize(relationshipOpeners[relationship] ?? relationshipOpeners.new, company);
    const fitSentence = fit.trim() ? `My experience with ${fit.trim()} seems relevant to the role.` : '';
    const references = [`Job ID: ${jobId}`, jobUrl.trim() ? `Job posting: ${jobUrl.trim()}` : '', resumeUrl.trim() ? `Resume: ${resumeUrl.trim()}` : ''].filter(Boolean).join('\n');
    const linkedIn = [`Hi ${recipient},`, `${opener} I’m preparing to apply for the ${role} position at ${company}.`, fitSentence, references,
        `If you feel my background could be a fit, would you be comfortable referring me? I’d be happy to share my resume and the job link. No pressure if you’re unable to.`,
        `Thank you for considering it,\n${sender || 'Your name'}`].filter(Boolean).join('\n\n');
    const instagram = [`Hi ${recipient}! ${opener}`, `I’m interested in the ${role} role at ${company}.${fitSentence ? ` ${fitSentence}` : ''}`,
        references, `If you’re comfortable, could you refer me or point me to the right person? I can send my resume and the role link. No worries if not — thank you!`, sender || 'Your name'].filter(Boolean).join('\n\n');
    return { linkedIn, instagram };
}
